package org.mashov

import org.apache.avro.Schema

object AvroUtils{
    fun primitiveField(field: Schema.Field): Boolean {
        val type = field.schema().type
        return type != Schema.Type.RECORD && type != Schema.Type.UNION
    }

    fun getUnionSchema(schema: Schema): Schema {
        return schema.types
                .filter { schema1: Schema -> schema1.type != Schema.Type.NULL }
                .reduce { a: Schema, b: Schema -> throw IllegalStateException("Multiple elements: $a, $b") }
    }
}