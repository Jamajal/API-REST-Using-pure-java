📌 Project Overview

This project is a minimal REST API built entirely from scratch using pure Java, without relying on external frameworks or libraries, to complement my network studies.

The main goal was to understand how HTTP communication and request handling work at a lower level — essentially exploring what happens under the hood of modern frameworks like Spring Boot.

The server is implemented using:

- ServerSocket to create and manage the HTTP server
- BufferedReader to read incoming request data
- PrintWriter to write HTTP responses back to the client

The application manually parses HTTP requests, interprets the operation and payload, and routes them accordingly.

⚙️ Features

- Basic HTTP request parsing
- Simple User CRUD operations
- Manual routing and response handling
- No external dependencies

🎯 Purpose

This project was created strictly for learning purposes.
The focus was not on production-level architecture, validation, or clean API design, but rather on understanding the core mechanics of how a REST API operates without abstraction layers.

Although it does not aim to replace professional frameworks, it provides valuable insight into how higher-level tools are structured internally.
