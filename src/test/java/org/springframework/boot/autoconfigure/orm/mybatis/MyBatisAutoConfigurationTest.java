package org.springframework.boot.autoconfigure.orm.mybatis;

import junit.framework.TestCase;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestDemoApplication.class)
public class MyBatisAutoConfigurationTest extends TestCase {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void contextLoads() throws Exception {
        Assert.assertNotNull(this.userMapper);
        int id = userMapper.addUser(new User("josh@spring.io", "jlong"));
        Assert.assertTrue(id > 0);

        User user = userMapper.getUser(id);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getEmail(), "josh@spring.io");
        Assert.assertEquals(user.getUserName(), "jlong");
        Assert.assertEquals(user.getId(), id);
    }
}

@EnableAutoConfiguration
@Configuration
class TestDemoApplication {
}

interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{userId}")
    User getUser(long userId);

    @Insert("insert into user (email, userName) values(#{email}, #{userName})")
    int addUser(User user);

}

class User {

    private int id;
    private String email;
    private String userName;

    public User() {
    }

    public User(String email, String userName) {
        this.email = email;
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
