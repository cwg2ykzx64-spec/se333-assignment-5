# Assignment 5 - CI Project

This project implements a simple Continuous Integration pipeline for a Java application using Maven, GitHub Actions, Checkstyle, and JaCoCo.

## Build Status

![SE333 CI Workflow](https://github.com/cwg2ykzx64-spec/se333-assignment-5/actions/workflows/SE333_Cl.yml/badge.svg)

## Project Overview

This project includes two main packages:
1.  **org.example.Barnes**: A simple simulation for purchasing books from a Barnes & Noble store.
2.  **org.example.Amazon**: A shopping cart simulation that uses multiple pricing rules and a database to calculate a final price.

The project is configured to automatically run static analysis and unit/integration tests on every push to the `main` branch via GitHub Actions.


* **GitHub Actions Workflow**: The workflow file is located at `.github/workflows/SE333_Cl.yml`.
* **Workflow Status**: The workflow has been configured to run. All actions are expected to pass.