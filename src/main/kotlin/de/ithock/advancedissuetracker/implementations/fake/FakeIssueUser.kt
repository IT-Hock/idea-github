package de.ithock.advancedissuetracker.implementations.fake

import com.github.javafaker.Faker
import de.ithock.advancedissuetracker.implementations.IssueUser

class FakeIssueUser : IssueUser() {
    init {
        this.id = Faker.instance().internet().uuid()
        this.username = Faker.instance().name().username()
        this.firstName = Faker.instance().name().firstName()
        this.lastName = Faker.instance().name().lastName()
        this.email = Faker.instance().internet().emailAddress()
        this.avatar = "https://randomuser.me/api/portraits/men/${Faker.instance().number().numberBetween(1, 100)}.jpg"
        this.profileUrl = "https://" + Faker.instance().internet().url()
    }
}