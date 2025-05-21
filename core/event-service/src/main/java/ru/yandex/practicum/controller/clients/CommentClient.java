package ru.yandex.practicum.controller.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feign.CommentServiceClient;

@FeignClient(name = "comment-service", path = "/admin/comments")
public interface CommentClient extends CommentServiceClient {
}