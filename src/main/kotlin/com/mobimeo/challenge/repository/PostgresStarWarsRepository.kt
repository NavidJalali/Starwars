package com.mobimeo.challenge.repository

import com.mobimeo.challenge.model.Asset
import com.mobimeo.challenge.model.Customer
import com.mobimeo.challenge.model.CustomerWithAssets
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class PostgresStarWarsRepository(
    private val client: DatabaseClient,
    private val transactionalOperator: TransactionalOperator
) : StarWarsRepository {
    override fun insertCustomer(customer: Customer): Mono<Customer> {
        return client.sql(
            """
            INSERT INTO customers (id, external_id, name) VALUES (:id, :external_id, :name)
            RETURNING id as customer_id, external_id as customer_external_id, name as customer_name
        """
        )
            .bind("id", customer.id)
            .bind("name", customer.name)
            .bind("external_id", customer.externalId)
            .fetch()
            .first()
            .map { Customer.unsafeFromMap(it) }
    }

    override fun insertAssets(assets: List<Asset>): Mono<Long> {
        // batch insert assets
        val inserts =
            Flux.concat(
                assets.map { asset ->
                    client.sql(
                        """
                    INSERT INTO assets (id, customer_id, name, type, external_id, value)
                    VALUES (:id, :customer_id, :name, :type, :external_id, :value)
                """
                    )
                        .bind("id", asset.id)
                        .bind("customer_id", asset.customerId)
                        .bind("name", asset.name)
                        .bind("external_id", asset.externalId)
                        .bind("type", asset.type.name)
                        .bind("value", asset.value)
                        .fetch()
                        .rowsUpdated()
                }
            )

        return transactionalOperator.transactional(inserts)
            .reduceWith({ 0L }, { left, right -> left + right })
    }

    override fun insertCustomerWithAssets(customerWithAssets: CustomerWithAssets): Mono<CustomerWithAssets> {
        return transactionalOperator.transactional (
            insertCustomer(customerWithAssets.customer)
                .flatMap { _ -> insertAssets(customerWithAssets.assets) }
                .map { _ -> customerWithAssets }
        )
    }

    override fun getAllCustomers(): Mono<List<Customer>> {
        return client.sql(
            """
            SELECT * FROM customers
        """
        )
            .fetch()
            .all()
            .collectList()
            .map { rows -> rows.map { Customer.unsafeFromMap(it) } }
    }

    override fun deleteAll(): Mono<Void> {
        return client.sql(
            """
            DELETE FROM assets
        """
        )
            .fetch()
            .rowsUpdated()
            .then(
                client.sql(
                    """
                    DELETE FROM customers;
                """
                )
                    .fetch()
                    .rowsUpdated()
            )
            .then()
    }

    override fun getCustomerWithAssets(externalId: Long): Mono<CustomerWithAssets> {
        return client.sql(
            """
            SELECT 
                c.id as customer_id,
                c.name as customer_name,
                c.external_id as customer_external_id,
                a.id as asset_id, 
                a.name as asset_name,
                a.type as asset_type,
                a.external_id as asset_external_id,
                a.customer_id as asset_customer_id,
                a.value as asset_value
            FROM customers c
            LEFT JOIN assets a ON a.customer_id = c.id
            WHERE c.external_id = :external_id
        """
        )
            .bind("external_id", externalId)
            .fetch()
            .all()
            .collectList()
            .mapNotNull { rows ->
                rows.map { row ->
                    Pair(Customer.unsafeFromMap(row), Asset.fromMap(row))
                }
                    .groupBy { it.first }
                    .map { (customer, pairs) ->
                        CustomerWithAssets(
                            customer = customer,
                            assets = pairs.mapNotNull { it.second }
                        )
                    }
                    .firstOrNull()
            }
    }
}
