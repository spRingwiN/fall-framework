package com.eric.fall.jdbc.with.tx;

import com.eric.fall.annotation.Autowired;
import com.eric.fall.annotation.Component;
import com.eric.fall.annotation.Transactional;
import com.eric.fall.jdbc.JdbcTemplate;
import com.eric.fall.jdbc.JdbcTestBase;

import java.util.List;

@Component
@Transactional
public class AddressService {

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void addAddress(Address... addresses) {
        for (Address address : addresses) {
            // if user not found will get an error
            userService.getUser(address.userId);
            jdbcTemplate.update(JdbcTestBase.INSERT_ADDRESS, address.userId, address.address, address.zip);
        }

    }

    public List<Address> getAddresses(int userId) {
        return jdbcTemplate.queryForList(JdbcTestBase.SELECT_ADDRESS_BY_USERID, Address.class, userId);
    }

    public void deleteAddress(int userId) {
        jdbcTemplate.update(JdbcTestBase.DELETE_ADDRESS_BY_USERID, userId);
        if (userId == 1) {
            throw new RuntimeException("Rollback delete for user id = 1");
        }
    }
















}
