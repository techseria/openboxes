package org.pih.warehouse.jobs

import grails.util.Holders as ConfigHolder
//import org.quartz.DisallowConcurrentExecution
import util.LiquibaseUtil

//@DisallowConcurrentExecution
class DataMigrationJob {

    def migrationService

    static triggers = { }

	def execute(context) {

        if (ConfigHolder.getGrailsApplication().config.openboxes.jobs.dataMigrationJob.enabled?:false) {

            if (LiquibaseUtil.isRunningMigrations()) {
                log.info "Postponing job execution until liquibase migrations are complete"
                return
            }

            log.info "Starting data migration job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            migrationService.migrateInventoryTransactions()
            log.info "Finished data migration job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }


}
