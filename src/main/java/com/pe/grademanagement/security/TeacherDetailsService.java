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
 * Used by Spring Security for authentication with Google OAuth.
 * 
 * Requirements:
 * - Load teacher details for authentication by email
 */
@Service
public class TeacherDetailsService implements UserDetailsService {
    
    private final TeacherRepository teacherRepository;
    
    @Autowired
    public TeacherDetailsService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find teacher by email (email is used as username in OAuth flow)
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found: " + email));
        
        // Return UserDetails with email as username, no password (OAuth), and empty authorities
        // We don't use role-based authorization, just authentication
        return User.builder()
                .username(teacher.getEmail())
                .password("") // No password for OAuth users
                .authorities(new ArrayList<>())
                .build();
    }
}
