package nl.fontys.s3.ticketwave_s3.Controller.InterfaceService;

import nl.fontys.s3.ticketwave_s3.Controller.DTOS.UserBehaviorDTO;

public interface UserBehaviorService {
    UserBehaviorDTO getUserBehavior(Integer userId);
    void warnUser(Integer userId);
    void banUser(Integer userId);
}
