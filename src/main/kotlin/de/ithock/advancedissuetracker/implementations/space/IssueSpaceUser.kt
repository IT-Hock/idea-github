package de.ithock.advancedissuetracker.implementations.space

import de.ithock.advancedissuetracker.implementations.IssueUser
import org.json.JSONObject

class IssueSpaceUser(jsonObject: JSONObject) : IssueUser() {

    init {
        id = jsonObject.getString("id")
        username = jsonObject.getString("username")
        firstName = jsonObject.getJSONObject("name").getString("firstName")
        lastName = jsonObject.getJSONObject("name").getString("lastName")
        val emails = jsonObject.getJSONArray("emails")
        for (i in 0 until emails.length()) {
            val email = emails.getJSONObject(i)
            if (!email.getBoolean("blocked")) {
                this.email = email.getString("email")
                break
            }
        }
        avatar = jsonObject.getString("avatar")
    }

    fun setValuesFromConnection(connection: SpaceConnection? = null) {
        if(connection == null) {
            return
        }

        avatar = "${connection.url}/d/${avatar}"
        profileUrl = "${connection.url}/m/${username}"
    }

    /*
    "createdBy": {
            "details": {
              "className": "CUserPrincipalDetails",
              "user": {
                "id": "3im4rv3Jm8Tz",
                "username": "subtixx",
                "name": {
                  "firstName": "Dominic",
                  "lastName": "Hock"
                },
                "smallAvatar": "ZZuIB23lt8E",
                "avatar": "4XD20p34fIi8",
                "emails": [
                  {
                    "email": "subtixx@users.noreply.github.com",
                    "blocked": false
                  },
                  {
                    "email": "admin@it-hock.de",
                    "blocked": false
                  },
                  {
                    "email": "d.hock@it-hock.de",
                    "blocked": false
                  },
                  {
                    "email": "subtixx@gmail.com",
                    "blocked": false
                  },
                  {
                    "email": "admin@example.com",
                    "blocked": false
                  }
                ]
              }
            }
          }
     */
}