# JSON Database - Java Client/Server Project

A Java client-server JSON database project built as part of the Hyperskill Java track.

This project simulates a simple remote database where a client sends JSON requests to a server over a socket connection. The server processes commands such as `set`, `get`, `delete`, and `exit`, then returns JSON responses.

## Features

- Client-server architecture using Java sockets
- JSON-based communication between client and server
- Supports `set`, `get`, `delete`, and `exit` commands
- Stores JSON values, including:
  - strings
  - numbers
  - arrays
  - nested JSON objects
- Supports nested keys using JSON arrays as paths
- Uses Gson for JSON parsing and formatting
- Maven-based project structure

## How It Works

The client creates a JSON request and sends it to the server.

Example request:

```json
{
  "type": "set",
  "key": ["person", "name"],
  "value": "Adam"
}