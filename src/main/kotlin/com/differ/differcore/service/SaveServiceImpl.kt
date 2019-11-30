package com.differ.differcore.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.models.Swagger
import org.springframework.stereotype.Service
import springfox.documentation.spring.web.DocumentationCache
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper
import java.io.File


@Service
internal open class SaveServiceImpl(
    private val documentationCache: DocumentationCache,
    private val objectMapper: ObjectMapper,
    private val mapper: ServiceModelToSwagger2Mapper
) : SaveService {

    private val defaultFileName = "differ-doc.json"

    override fun start() {
        documentationCache.all()
            .entries
            .map { it.key to mapper.mapDocumentation(it.value) }
            .toMap()
            .let { saveMap(it) }
    }

    private fun saveMap(it: Map<String, Swagger>) =
        objectMapper.writeValue(File(defaultFileName), it)
}