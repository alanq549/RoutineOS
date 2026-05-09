package com.alan.routineos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.core.database.AppDatabase
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var nodeTypeDao: NodeTypeDao
    private lateinit var metadataSchemaDao: MetadataSchemaDao
    private lateinit var nodeDao: NodeDao
    private lateinit var fieldValueDao: FieldValueDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        nodeTypeDao = db.nodeTypeDao()
        metadataSchemaDao = db.metadataSchemaDao()
        nodeDao = db.nodeDao()
        fieldValueDao = db.fieldValueDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeAndReadGenericNode() = runBlocking {
        // 1. Crear un NodeType "Ejercicio"
        val exerciseType = NodeType(
            name = "Ejercicio",
            hasMetricFields = true,
            allowsChildren = false
        )
        nodeTypeDao.upsert(exerciseType)

        // 2. Crear 3 schemas (series, reps, peso)
        val schemaSeries = NodeMetadataSchema(
            typeId = exerciseType.id,
            fieldName = "series",
            fieldLabel = "Series",
            fieldType = FieldType.NUMBER,
            defaultValue = "3"
        )
        val schemaReps = NodeMetadataSchema(
            typeId = exerciseType.id,
            fieldName = "reps",
            fieldLabel = "Repeticiones",
            fieldType = FieldType.NUMBER,
            defaultValue = "10"
        )
        val schemaWeight = NodeMetadataSchema(
            typeId = exerciseType.id,
            fieldName = "peso_kg",
            fieldLabel = "Peso",
            fieldType = FieldType.NUMBER,
            unit = "kg",
            stepSize = 2.5f
        )
        metadataSchemaDao.upsert(schemaSeries)
        metadataSchemaDao.upsert(schemaReps)
        metadataSchemaDao.upsert(schemaWeight)

        // 3. Crear un Node de ese tipo
        val node = Node(
            typeId = exerciseType.id,
            name = "Press Banca"
        )
        nodeDao.upsert(node)

        // 4. Guardar 2 NodeFieldValues
        val valueWeight = NodeFieldValue(
            nodeId = node.id,
            schemaId = schemaWeight.id,
            fieldName = "peso_kg",
            value = "80"
        )
        val valueSeries = NodeFieldValue(
            nodeId = node.id,
            schemaId = schemaSeries.id,
            fieldName = "series",
            value = "4"
        )
        fieldValueDao.upsert(valueWeight)
        fieldValueDao.upsert(valueSeries)

        // 5. Los lee de vuelta correctamente
        val savedNode = nodeDao.getById(node.id)
        assertNotNull(savedNode)
        assertEquals("Press Banca", savedNode?.name)

        val values = fieldValueDao.getByNode(node.id).first()
        assertEquals(2, values.size)
        
        val weightVal = values.find { it.fieldName == "peso_kg" }
        assertEquals("80", weightVal?.value)
        
        val seriesVal = values.find { it.fieldName == "series" }
        assertEquals("4", seriesVal?.value)
        
        val schemas = metadataSchemaDao.getByTypeId(exerciseType.id).first()
        assertEquals(3, schemas.size)
    }
}
