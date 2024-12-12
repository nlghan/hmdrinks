package com.hmdrinks.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "favourite_item")
public class FavouriteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favItemId", nullable = false)
    private int favItemId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "proId", referencedColumnName = "proId"),
            @JoinColumn(name = "size", referencedColumnName = "size")
    })
    private ProductVariants productVariants;

    @ManyToOne
    @JoinColumn(name = "favId", nullable = false)
    private Favourite favourite;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted",columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;
}

