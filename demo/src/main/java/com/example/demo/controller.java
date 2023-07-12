package com.example.demo;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sun.tools.jconsole.JConsoleContext;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.expression.ExpressionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.*;

import org.springframework.web.server.ResponseStatusException;


@RestController

public class controller {
    /**
     * Object Class
     * Implement HashCode, Equals
     * Collection (Data Structure)
     *
     * @return
     */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @RequestMapping("/hello")
    @GetMapping()
    public String getUserInput() {

        return "Hello";
    }

    String SECRET_KEY = "MXAwam5xYjEyM3U4MG5mYXNkZjEzNXNsZmpmYTAxOGNiZGpkbW4xeTM3OTU2";

    @PostMapping(value = "/user")
    public User createUser(@RequestBody User newUser) {
        try {
            userRepository.save(newUser);
            return newUser;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PutMapping(value = "/user")
    public boolean updateUser(@RequestBody int ID, @RequestBody User user, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            User newUser = userRepository.findById(ID).get();
            newUser.setName(user.getName());
            newUser.setPassword(user.getPassword());
            newUser.setIsSeller(user.getIsSeller());
            userRepository.save(newUser);
            return true;
        } catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }

    @GetMapping(value = "/user")
    public @ResponseBody Optional<User> findUser(@RequestParam int id, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            return userRepository.findById(id);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }

    @PostMapping(value = "/item")
    public Item createItem(@RequestBody Item newItem, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            if (claims.getSubject().equals("Admin")) {
                int sellerId = Integer.parseInt(claims.getIssuer());
                newItem.setSeller(sellerId);
                itemRepository.save(newItem);
                User newUser = userRepository.findById(sellerId).get();
                newUser.setNewItem(true);
                userRepository.save(newUser);
                return newItem;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
            }
        } catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }

    @PutMapping(value = "/item")
    public boolean updateItem(@RequestParam int ID, @RequestBody Item item, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            if (claims.getSubject().equals("Admin")) {
                Item newItem = itemRepository.findById(ID).get();
                newItem.setName(item.getName());
                newItem.setDescription(item.getDescription());
                newItem.setImageURL(item.getImageURL());
                newItem.setPrice(item.getPrice());
                itemRepository.save(newItem);
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
            }
        } catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }

    @GetMapping(value = "/item")
    public @ResponseBody Optional<Item> findItem(@RequestParam int id, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            return itemRepository.findById(id);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }
    @GetMapping(value = "/deleteUser")
    public Boolean deleteUser(@RequestParam int id, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            if (claims.getSubject().equals("Admin")) {
                userRepository.deleteById(id);
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
            }
        } catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }
    @GetMapping(value="/allUsers")
    public @ResponseBody Iterable<User> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            return userRepository.findAll();

        } catch (Exception e){
            System.out.println(e);
            System.out.println(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }
    @PostMapping(value = "/auth")
    public String authUser(@RequestBody Map<String, String> json) {
        Iterable<User> userList = userRepository.findAll();
        Iterator<User> userIterator = userList.iterator();
        boolean auth = false;
        User actualUser = new User();
        while(userIterator.hasNext()) {
            User u = userIterator.next();
            if (u.getName().toLowerCase().equals(json.get("user").toLowerCase()) && u.getPassword().equals(json.get("password"))) {
                auth = true;
                actualUser = u;
                break;
            }
        }
        String adminString = "User";
        if (actualUser.getIsSeller()) {
            adminString = "Admin";
        }
        if (auth) {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            JwtBuilder builder = Jwts.builder().setId(actualUser.getId().toString())
                    .setSubject(adminString)
                    .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                    .setIssuer(actualUser.getId().toString())
                    .signWith(signatureAlgorithm, SECRET_KEY.getBytes());
            return builder.compact();
        } else {
            return "false";
        }
    }

    @GetMapping(value = "/deleteItem")
    public Boolean deleteItem(@RequestParam int id, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            if (claims.getSubject().equals("Admin")) {
                itemRepository.deleteById(id);
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
            }
        } catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "YOU DO NOT HAVE PERMISSION TO MAKE THIS CALL");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID OR EXPIRED TOKEN");
        }
    }
    @GetMapping(value="/allItems")
    public @ResponseBody Iterable<Item> getAllItems(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.split(" ")[1]).getBody();
            Iterable<User> userList = userRepository.findAll();
            Iterator<User> userIterator = userList.iterator();
            boolean auth = false;
            while(userIterator.hasNext()) {
                User u = userIterator.next();
                if (u.getNewItem()) {
                    u.setNewItem(false);
                    userRepository.save(u);
                }
            }
            return itemRepository.findAll();
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}


