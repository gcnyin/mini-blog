import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

val encoder = new BCryptPasswordEncoder()

val password = "123456"

encoder.encode(password)
