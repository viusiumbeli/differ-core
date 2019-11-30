package com.differ.differcore.service

import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import javax.annotation.PostConstruct

@Service
class VersionServiceImpl(
    private val resourceLoader: ResourceLoader
) : VersionService {

    private lateinit var jversions: File

    @PostConstruct
    fun init() {
        jversions = resourceLoader.getResource(LOCATION).file
    }

    override fun getAllVersions(): List<String> {
        return jversions
            .listFiles()
            ?.map { it.name }
            ?.map { it.removeSuffix(".json") }
            ?: Collections.emptyList()
    }

    override fun getLastVersionFile(): File? {
        return jversions
            .listFiles()
            ?.max()
    }

    override fun getPenultimateVersionFile(): File? {
        val sortedVersions = jversions.listFiles()?.sorted()
        return takeIf { sortedVersions != null && sortedVersions.size > 1 }?.let { sortedVersions!![sortedVersions.size - 2] }
    }

    override fun getVersionFile(version: String): File? =
        resourceLoader.getResource("$LOCATION${File.separator}$version.json").takeIf { it.exists() }?.file

    companion object {
        private const val LOCATION = "classpath:jversions"
    }
}