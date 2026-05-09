package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.MetadataSchemaDao
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataSchemaRepository @Inject constructor(
    private val metadataSchemaDao: MetadataSchemaDao
) {
    fun getByTypeId(typeId: String): Flow<List<NodeMetadataSchema>> = 
        metadataSchemaDao.getByTypeId(typeId)

    suspend fun upsert(schema: NodeMetadataSchema) = metadataSchemaDao.upsert(schema)

    suspend fun delete(schema: NodeMetadataSchema) = metadataSchemaDao.delete(schema)
}
