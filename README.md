merkledag-server
================

**WORK IN PROGRESS**

This project implements a REST API for interacting with a
[merkledag repository](https://github.com/greglook/clj-merkledag) over HTTP.

## API Resources

The REST API is divided into sets of resources. First are the block routes,
which let the client interact directly with raw block data:

```
GET    /blocks/       List blocks (with pagination)
POST   /blocks/       Store a new raw block
HEAD   /blocks/:id    Get block stats and storage metadata
GET    /blocks/:id    Get a block's content
DELETE /blocks/:id    Remove a block from the store (may not be allowed)
```

Next are the nodes resources, which read blocks and attempt to interpret the
data inside.

```
POST   /nodes/             Create a node by providing structured data.
HEAD   /nodes/:id          Get the storage metadata, encoding, and links of a block without the content
GET    /nodes/:id          Get the links and content of a node
GET    /nodes/:id/:path*   Traverse link paths and return the final node.
```

The 'refs' routes provide mutable references into the graph, which can be
updated by processes over time.

```
GET    /refs/         List named reference pointers.
GET    /refs/:name    Resolve the given reference; returns the hash of the target node.
PUT    /refs/:name    Create or update a pointer.
DELETE /refs/:name    Remove a reference pointer.
```

Finally, there are a few system routes which provide information and control
over the server:

```
GET    /sys/info    Retrieve information about the system status.
GET    /sys/ping    Simple health-check endpoint to verify the server is in a good state.
```

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
