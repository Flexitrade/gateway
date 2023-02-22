package de.flexitrade.gateway.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

        System.out.print("Password admin: ");
        System.out.println(bcrypt.encode("admin"));

        System.out.print("Password test: ");
        System.out.println(bcrypt.encode("test"));
    }

}
