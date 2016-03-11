merkledag-server
================

**WORK IN PROGRESS**

This project implements a REST API for interacting with a
[merkledag repository](https://github.com/greglook/clj-merkledag) over HTTP.

## API Resources

The REST API is divided into sets of resources.

### Blocks

The block routes let clients interact directly with raw block data.

```
GET    /blocks/       List blocks (with pagination)
POST   /blocks/       Store a new raw block
HEAD   /blocks/:id    Get block stats and storage metadata
GET    /blocks/:id    Get a block's content
DELETE /blocks/:id    Remove a block from the store (may not be allowed)
```

### Nodes

The node routes read blocks and attempt to interpret the data inside. Node
resources include the id and size of the encoded block, the list of multicodec
headers read from the block, and the encoded links and data if present.

```
POST   /nodes/             Create a node by providing structured data.
GET    /nodes/:id          Get the links and content of a node
GET    /nodes/:id/:path*   Traverse link paths and return the final node.
```

### Refs

The ref routes provide mutable references into the graph which can be updated
over time. These are conceptually similar to git's branches or tags.

```
GET    /refs/           List named reference pointers.
GET    /refs/:name      Resolve the given reference.
GET    /refs/:name/log  Return historical values for the reference.
PUT    /refs/:name      Create or update a pointer.
DELETE /refs/:name      Remove a reference pointer.
```

### System

Finally, there are a few system routes which provide information and control
over the server:

```
GET    /sys/info    Retrieve information about the system status.
GET    /sys/ping    Simple health-check endpoint to verify the server is in a good state.
```

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
