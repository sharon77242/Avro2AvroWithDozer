package org.mashov

import org.apache.avro.Schema
import org.apache.avro.specific.SpecificData
import org.apache.avro.specific.SpecificRecord

object AvroUtils {
    fun Schema.Field.required(): Boolean {
        return !this.hasDefaultValue()
    }

    fun Schema.Field.primitive(): Boolean {
        val type = this.schema().type
        return type != Schema.Type.RECORD && type != Schema.Type.UNION
    }

    fun Schema.getUnionSchema(): Schema {
        return this.types
                .filter { schema1: Schema -> schema1.type != Schema.Type.NULL }
                .reduce { a: Schema, b: Schema -> throw IllegalStateException("Multiple elements: $a, $b") }
    }

    fun Schema.createNewInstance(): SpecificRecord {
        var schemaCopy: Schema = this
        if (schemaCopy.isUnion) {
            schemaCopy = schemaCopy.getUnionSchema()
        }
        return SpecificData.newInstance(Class.forName(schemaCopy.fullName), schemaCopy) as SpecificRecord
    }

    fun SpecificRecord.createAndValidateRecord(): SpecificRecord {
        val recordBuilder = this.javaClass.getMethod("newBuilder", this.javaClass)
                .invoke(this, this)
        return recordBuilder.javaClass.getMethod("build")
                .invoke(recordBuilder) as SpecificRecord
    }
}