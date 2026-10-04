package com.test.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.Param;
import com.framework.annotation.UrlMapping;

@Controller
public class EmployeController {

    @UrlMapping("/emp/list")
    public String list() {
        return "Liste des employés";
    }

    @UrlMapping("/emp/new")
    public String create() {
        return "Création d'un employé";
    }

    @UrlMapping("/andrana")
    public void andrana() {

     }
    }
