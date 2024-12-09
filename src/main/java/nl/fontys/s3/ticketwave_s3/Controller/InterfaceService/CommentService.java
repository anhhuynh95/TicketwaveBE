package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.CommentDTO;

import java.util.List;

public interface CommentService {
    CommentDTO addComment(CommentDTO commentDTO);
    List<CommentDTO> getCommentsByEventId(Integer eventId);
    void deleteComment(Integer commentId);

}
