package com.empresa.demo.service;

import org.springframework.stereotype.Service;

import com.empresa.demo.model.User;

@Service
public class UserService {
    public User findById(Long id) {
        // TODO: simular recuperación (p.ej. base de datos)
        return new User(id, "NombreEjemplo", "ejemplo@empresa.com");
    }
}
