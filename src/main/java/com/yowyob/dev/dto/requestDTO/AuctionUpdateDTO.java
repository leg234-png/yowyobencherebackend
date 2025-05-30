package com.yowyob.dev.dto.requestDTO;


import com.yowyob.dev.enumeration.AuctionStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AuctionUpdateDTO {

    private String title;

    private String description;

    private Double startingPrice;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private List<MultipartFile> images;

    private String itemCondition;

    private UUID category_id;

    private AuctionStatus status;
}
