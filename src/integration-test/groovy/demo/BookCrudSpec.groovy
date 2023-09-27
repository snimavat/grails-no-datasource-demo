package demo

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class BookCrudSpec extends Specification {

    void "create"() {
        when:
        new Book(name:"B1", price: 10.0).save(flush:true)

        then:
        noExceptionThrown()

        and:
        Book.findByName("B1") != null
    }
}
