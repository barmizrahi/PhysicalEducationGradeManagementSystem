package com.pe.grademanagement.security;

import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * UserDetailsService implementation for loading teacher details.
 * Used by Spring Security for authentication.
 * 
 * Requirements:
 * - 13.1: Load teacher details for authentication
 */
@Service
public class TeacherDetailsService implements UserDetailsService {
    
    private final TeacherRepository teacherRepository;
    
    @Autowired
    public TeacherDetailsService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find teacher by username
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found: " + username));
        
        // Return UserDetails with username, password, and empty authorities
        // We don't use role-based authorization, just authentication
        return User.builder()
                .username(teacher.getUsername())
                .password(teacher.getPasswordHash())
                .authorities(new ArrayList<>())
                .build();
    }
}
