package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.utils.pagination.PageRequest
import test.ktortemplate.core.utils.pagination.createSorts
import test.ktortemplate.core.utils.pagination.fromFilters

class CarRepositoryImpl : CarRepository {

    override fun exists(id: Long): Boolean {
        return CarMappingsTable.select { CarMappingsTable.id eq id }.count() == 1L
    }

    override fun getById(id: Long): Car? {
        val rst = CarMappingsTable.select { CarMappingsTable.id eq id }.singleOrNull()
        return if (rst != null) {
            resultToModel(rst)
        } else {
            null
        }
    }

    override fun save(car: CarSaveCommand): Car {
        val newCarId = CarMappingsTable.insert {
            it[brand] = car.brand
            it[model] = car.model
        } get CarMappingsTable.id

        return Car(newCarId.value, car.brand, car.model)
    }

    override fun update(car: Car): Car {
        CarMappingsTable.update({ CarMappingsTable.id eq car.id }) {
            it[brand] = car.brand
            it[model] = car.model
        }
        return getById(car.id)!!
    }

    override fun count(pageRequest: PageRequest): Int {
        return CarMappingsTable.fromFilters(pageRequest.filter).count().toInt()
    }

    override fun delete(id: Long) {
        CarMappingsTable.deleteWhere { CarMappingsTable.id eq id }
    }

    override fun list(pageRequest: PageRequest): List<Car> {
        return CarMappingsTable
            .fromFilters(pageRequest.filter)
            .limit(pageRequest.limit, pageRequest.offset.toLong())
            .orderBy(*CarMappingsTable.createSorts(pageRequest.sort).toTypedArray())
            .map { resultToModel(it) }
    }

    private fun resultToModel(rstRow: ResultRow): Car {
        return Car(
            rstRow[CarMappingsTable.id].value,
            rstRow[CarMappingsTable.brand],
            rstRow[CarMappingsTable.model]
        )
    }
}
