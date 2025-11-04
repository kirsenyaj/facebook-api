package com.villanueva.facebookapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;

/**
 * All controller/repository/entity/application classes live in the same package
 * as requested.
 *
 * Endpoints:
 * GET    /api/posts          - list all posts
 * GET    /api/posts/{id}     - get a single post
 * POST   /api/posts          - create a post
 * PUT    /api/posts/{id}     - update a post
 * DELETE /api/posts/{id}     - delete a post
 */
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173") // allow Vite dev server during development
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // DTOs are simple inner static classes to avoid adding new packages
    public static class CreatePostRequest {
        public String author;
        public String content;
        public String imageUrl;
    }

    public static class UpdatePostRequest {
        public String content;
        public String imageUrl;
    }

    @GetMapping
    public List<Post> listAll() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getOne(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreatePostRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "request body is required"));
        }
        if (!StringUtils.hasText(req.author)) {
            return ResponseEntity.badRequest().body(Map.of("error", "author is required"));
        }
        if (!StringUtils.hasText(req.content)) {
            return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
        }

        Post p = new Post();
        p.setAuthor(req.author.trim());
        p.setContent(req.content.trim());
        p.setImageUrl(req.imageUrl);
        Post saved = postRepository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody UpdatePostRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "request body is required"));
        }

        return postRepository.findById(id)
                .map(existing -> {
                    if (req.content != null) {
                        if (!StringUtils.hasText(req.content)) {
                            return ResponseEntity.badRequest().body(Map.of("error", "content cannot be blank"));
                        }
                        existing.setContent(req.content);
                    }
                    if (req.imageUrl != null) existing.setImageUrl(req.imageUrl);
                    Post updated = postRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(existing -> {
                    postRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}