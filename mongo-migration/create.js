db = db.getSiblingDB('blog')
db.posts.drop()
db.posts.insertOne({ title: "Hello world", content: "test", created: 0 })
db.posts.createIndex({ created: 1 })

db.users.drop()
db.users.insertOne({
    username: "admin",
    password: "$2a$10$5p96u0P9lAEpN4hfT9vMke.sX2RwiCmQsPiTC.z7k.I.0etjMCl.S"
})
