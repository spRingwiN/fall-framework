package com.eric.fall.jdbc.with.tx;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.exception.TransactionalException;
import com.eric.fall.jdbc.JdbcTemplate;
import com.eric.fall.jdbc.JdbcTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcWithTxTest extends JdbcTestBase {

    @Test
    public void testJdbcWithTx() {
        try (var ctx = new AnnotationConfigApplication(JdbcWithTxApplication.class, createPropertyResolver())) {
            JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
            jdbcTemplate.update(CREATE_USER);
            jdbcTemplate.update(CREATE_ADDRESS);

            UserService userService = ctx.getBean(UserService.class);
            AddressService addressService = ctx.getBean(AddressService.class);
            // proxied
            assertNotSame(UserService.class, userService.getClass());
            assertNotSame(AddressService.class, addressService.getClass());
            // proxy object is not inject:
            assertNull(userService.addressService);
            assertNull(addressService.userService);

            // insert user:
            User bob = userService.crerateUser("Bob", 12);
            assertEquals(1, bob.id);

            // insert address:
            Address addr1 = new Address(bob.id, "China qingdao", 10012);
            Address addr2 = new Address(bob.id, "China shanghai", 10080);
            // Note user not exist for addr3
            Address addr3 = new Address(bob.id + 1, "China beijing", 10001);
            assertThrows(TransactionalException.class, () -> addressService.addAddress(addr1, addr2, addr3));

            // All address should not inserted:
            assertTrue(addressService.getAddresses(bob.id).isEmpty());
            // insert addr1 addr2 for Bob only:
            addressService.addAddress(addr1, addr2);
            assertEquals(2, addressService.getAddresses(bob.id).size());

            // now delete bob will cause rollback
            assertThrows(TransactionalException.class, () -> userService.deleteUser(bob));

            // bob and his address still exist
            assertEquals("Bob", userService.getUser(1).name);
            assertEquals(2, addressService.getAddresses(1).size());
            System.out.println(userService.greet());
        }

        // re-open db and query:
        try (var ctx = new AnnotationConfigApplication(JdbcWithTxApplication.class, createPropertyResolver())) {
            AddressService addressService = ctx.getBean(AddressService.class);
            assertEquals(2, addressService.getAddresses(1).size());
        }
































    }



}
