package dev.u9g

import com.eclipsesource.json.Json
import com.google.common.base.Splitter
import com.mojang.logging.LogUtils
import dev.u9g.EmbeddedZipResourcePack.Companion.make
import net.minecraft.resource.*
import net.minecraft.resource.ResourcePackProfile.PackFactory
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.resource.metadata.ResourceMetadataReader
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.PathUtil
import org.cryptonode.jncryptor.AES256JNCryptor
import org.cryptonode.jncryptor.CryptorException
import org.cryptonode.jncryptor.JNCryptor
import org.slf4j.Logger
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

var momicaResourcePackBytes: ByteArray? = null

const val SKY_PLUS_FOLDER = "skyplus"

const val MOMICA_TEXTUREPACK_NAME = "dc4b57fc276a1b1b55c70dda5dc5f4d9"

val SKY_PLUS_FOLDER_PATH = Path("./", SKY_PLUS_FOLDER).also { it.createDirectories() }

val MOMICA_PACK_PATH: Path = SKY_PLUS_FOLDER_PATH.resolve(MOMICA_TEXTUREPACK_NAME)

fun makeResourcePackProfile(packName: String, packNameText: Text): ResourcePackProfile {
    val bytes = if (momicaResourcePackBytes == null) {
        val cryptor: JNCryptor = AES256JNCryptor()

        if (Files.exists(MOMICA_PACK_PATH)) {
            val encryptedBytes = Files.readAllBytes(MOMICA_PACK_PATH)
            val decryptedBytes = cryptor.decryptData(encryptedBytes, "secretsquirrel".toCharArray())
            momicaResourcePackBytes = decryptedBytes

            decryptedBytes
        } else {
            val downloadedBytes =
                URL("https://cdn.modrinth.com/data/PUUpX2qq/versions/vUKNuWSb/Alacrity.zip").readBytes()
            momicaResourcePackBytes = downloadedBytes

            try {
                val ciphertext: ByteArray = cryptor.encryptData(downloadedBytes, "secretsquirrel".toCharArray())
                Files.write(MOMICA_PACK_PATH, ciphertext, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            } catch (e: CryptorException) {
                // Something went wrong
                e.printStackTrace()
            }

            downloadedBytes
        }
    } else {
        momicaResourcePackBytes
    }

    val byteData =
        ByteArrayInputStream(bytes)
    val pack = make(
        packName,
        byteData
    )

    return ResourcePackProfile.of(
        pack.name, packNameText, false,
        object : PackFactory {
            override fun open(name: String): ResourcePack {
                return pack
            }

            override fun openWithOverlays(name: String, metadata: ResourcePackProfile.Metadata): ResourcePack {
                return pack
            }
        },
        ResourcePackProfile.Metadata(
            pack.description(),
            ResourcePackCompatibility.COMPATIBLE,
            FeatureSet.of(FeatureFlags.VANILLA),
            listOf()
        ), ResourcePackProfile.InsertionPosition.TOP, true,
        ResourcePackSource.BUILTIN
    )
}

class EmbeddedZipResourcePack(private val name: String, private val data: Map<String, ByteArray>) :
    ResourcePack {

    override fun openRoot(vararg segments: String): InputSupplier<InputStream>? {
        PathUtil.validatePath(*segments)

        val bytes = data[java.lang.String.join("/", *segments)]

        return if (bytes == null) null else InputSupplier { ByteArrayInputStream(bytes) }
    }

    override fun open(type: ResourceType, id: Identifier): InputSupplier<InputStream>? {
        val bytes = data[getFilename(type, id)]

        return if (bytes == null) null else InputSupplier { ByteArrayInputStream(bytes) }
    }

    override fun findResources(
        type: ResourceType,
        namespace: String,
        prefix: String,
        consumer: ResourcePack.ResultConsumer
    ) {
        val string = type.directory + "/" + namespace + "/"
        val string2 = "$string$prefix/"
        for (name in data.keys) {
            if (name.endsWith("/") || name.endsWith(".mcmeta") || !name.startsWith(string2)) continue
            val string4 = name.substring(string.length)
            val identifier: Identifier? = Identifier.of(namespace, string4)
            if (identifier == null) {
                LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, string4)
                continue
            }

            consumer.accept(identifier, open(type, identifier))
        }
    }

    override fun getNamespaces(type: ResourceType): Set<String> {
        val set: MutableSet<String> = mutableSetOf()
        for (name in data.keys) {
            val list: List<String> = if (!name.startsWith(type.directory + "/")) {
                continue
            } else {
                val values = TYPE_NAMESPACE_SPLITTER.split(name).toList()
                if (values.size <= 1) {
                    continue
                }
                values
            }

            val namespace = list[1]
            if (namespace == namespace.lowercase(Locale.ROOT)) {
                set.add(namespace)
                continue
            }
            LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", namespace, name)
        }

        return set
    }

    override fun <T> parseMetadata(metaReader: ResourceMetadataReader<T>): T? {
        return AbstractFileResourcePack.parseMetadata(metaReader, ByteArrayInputStream(data["pack.mcmeta"]))
    }


    override fun getName(): String {
        return name
    }

    override fun close() {}

    fun description(): Text {
        return Text.of(
            Json.parse(
                ByteArrayInputStream(
                    data["pack.mcmeta"]
                ).readAllBytes().toString(Charset.forName("UTF-8"))
            )
                .asObject().get("pack").asObject()
                .get("description").asString()
        )
    }

    companion object {
        private val LOGGER: Logger = LogUtils.getLogger()
        val TYPE_NAMESPACE_SPLITTER: Splitter = Splitter.on('/').omitEmptyStrings().limit(3)

        private fun getFilename(type: ResourceType, id: Identifier): String {
            return java.lang.String.format(Locale.ROOT, "%s/%s/%s", type.directory, id.getNamespace(), id.getPath())
        }

        fun make(name: String, data: InputStream): EmbeddedZipResourcePack {
            return EmbeddedZipResourcePack(name, ZipInputStream(data)
                .use { zipInputStream ->
                    generateSequence { zipInputStream.nextEntry }
                        .filterNot { it.isDirectory }
                        .map {
                            Pair(it.name, zipInputStream.readAllBytes())
                        }.toMap()
                })
        }
    }
}