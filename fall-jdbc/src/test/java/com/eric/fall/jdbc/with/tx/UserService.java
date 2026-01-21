package com.eric.fall.jdbc.with.tx;

import com.eric.fall.annotation.Autowired;
import com.eric.fall.annotation.Component;
import com.eric.fall.annotation.Transactional;
import com.eric.fall.jdbc.JdbcTemplate;
import com.eric.fall.jdbc.JdbcTestBase;

@Component
@Transactional
public class UserService {

    @Autowired
    AddressService addressService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public User crerateUser(String name, int age) {
        Number id = jdbcTemplate.updateAndReturnGeneratedKey(JdbcTestBase.INSERT_USER, name, age);
        User user = new User();
        user.id = id.intValue();
        user.name = name;
        user.theAge = age;
        return user;
    }

    public User getUser(int userId) {
        return jdbcTemplate.queryForObject(JdbcTestBase.SELECT_USER, User.class, userId);
    }

    public void updateUser(User user) {
        jdbcTemplate.update(JdbcTestBase.UPDATE_USER, user.name, user.theAge, user.id);
    }

    public void deleteUser(User user) {
        jdbcTemplate.update(JdbcTestBase.DELETE_USER, user.id);
        addressService.deleteAddress(user.id);

    }

     public final String greet() {
        // not enhanced, injection properties are null, and the class name is the proxied class
         System.out.println("=============" + this.addressService);
        System.out.println("=============" + this.getClass().getName());
        return "hello world";
    }













}
