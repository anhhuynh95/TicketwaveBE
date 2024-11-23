package nl.fontys.s3.ticketwave_s3.Repository;

import nl.fontys.s3.ticketwave_s3.Domain.UserRole;
import nl.fontys.s3.ticketwave_s3.Mapper.UserMapper;
import nl.fontys.s3.ticketwave_s3.Repository.Entity.UserEntity;
import nl.fontys.s3.ticketwave_s3.Repository.JPA.UserDBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({UserRepositoryImpl.class, UserMapper.class}) // Import required beans
class UserRepositoryImplTest {

    @Autowired
    private UserDBRepository userDBRepository;

   @BeforeEach
    public void setUp() {
        // Insert a test user in the in-memory database
        UserEntity testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$S3WJuqnb2WyDICX2UE8mfO73NffyTGKYUPXKvh9YpBLsdGWoma0Fa");
        testUser.setRole(UserRole.USER);
        testUser.setActive(true);
        userDBRepository.save(testUser);
    }

    @Test
    void testFindByUsername() {
        // Act
        UserEntity userEntity = userDBRepository.findByUsername("testuser")
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Log the user details
        System.out.println("Username: " + userEntity.getUsername());
        System.out.println("Password: " + userEntity.getPassword());
        System.out.println("Role: " + userEntity.getRole());

        // Assert the values
        assertEquals("testuser", userEntity.getUsername());
        assertEquals("$2a$10$S3WJuqnb2WyDICX2UE8mfO73NffyTGKYUPXKvh9YpBLsdGWoma0Fa", userEntity.getPassword());
        assertEquals(UserRole.USER, userEntity.getRole());
    }

}
