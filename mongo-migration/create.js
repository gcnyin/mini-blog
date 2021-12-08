db = db.getSiblingDB('blog')
db.posts.drop()
db.posts.insertOne({title: "Hello world", content: "test", created: 0})
db.posts.createIndex({created: 1})
