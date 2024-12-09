package com.hmdrinks.Service;

import com.hmdrinks.Entity.Post;
import com.hmdrinks.Entity.PriceHistory;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.PriceHistoryRepository;
import com.hmdrinks.Repository.ProductVariantsRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PriceHistoryService {
    @Autowired
    private PriceHistoryRepository priceHistoryRepository;
    @Autowired
    private ProductVariantsRepository productVariantsRepository;

    public ResponseEntity<?> getListAllHistoryPriceByProductVarId(String pageFromParam, String limitFromParam, int productVarId) {
        ProductVariants productVariants = productVariantsRepository.findByVarId(productVarId);
        if(productVariants == null){
            return new ResponseEntity<>("No Product Variant found", HttpStatus.NOT_FOUND);
        }
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<PriceHistory> priceHistories = priceHistoryRepository.findByProductVariant_VarId(productVarId, pageable);
        List<PriceHistory> priceHistories1 = priceHistoryRepository.findByProductVariant_VarId(productVarId);
        List<PriceHistoryResponse> priceHistoryResponses = new ArrayList<>();
        for(PriceHistory priceHistory : priceHistories) {
            priceHistoryResponses.add(new PriceHistoryResponse(
                         priceHistory.getHistoryId(),
                         priceHistory.getProductVariant().getVarId(),
                         priceHistory.getDateChanged(),
                         priceHistory.getOldPrice(),
                         priceHistory.getNewPrice()
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListAllPriceHistoryByVarIdResponse(
                        page,
                        priceHistories.getTotalPages(),
                        limit,
                        priceHistories1.size(),
                        priceHistoryResponses
                )
        );
    }

    public ResponseEntity<?> getListAllHistoryPrice(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<PriceHistory> priceHistories = priceHistoryRepository.findAll(pageable);
        List<PriceHistory> priceHistories1 = priceHistoryRepository.findAll();
        List<PriceHistoryResponse> priceHistoryResponses = new ArrayList<>();
        for(PriceHistory priceHistory : priceHistories) {
            priceHistoryResponses.add(new PriceHistoryResponse(
                    priceHistory.getHistoryId(),
                    priceHistory.getProductVariant().getVarId(),
                    priceHistory.getDateChanged(),
                    priceHistory.getOldPrice(),
                    priceHistory.getNewPrice()
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListAllPriceHistoryByVarIdResponse(
                        page,
                        priceHistories.getTotalPages(),
                        limit,
                        priceHistories1.size(),
                        priceHistoryResponses
                )
        );
    }
}