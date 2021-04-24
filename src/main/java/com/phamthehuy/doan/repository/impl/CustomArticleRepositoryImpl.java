package com.phamthehuy.doan.repository.impl;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.repository.CustomArticleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Repository
public class CustomArticleRepositoryImpl implements CustomArticleRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Article> findCustom(String sort, Long start, Long end,
                                    Integer ward, Integer district, Integer city,
                                    Boolean roommate, String status, Boolean vip, String search,
                                    Integer minAcreage, Integer maxAcreage) {
        if (search == null || search.trim().equals("")) search = "";

        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Article> query = builder.createQuery(Article.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<Article> root = query.from(Article.class);

        //xác định cột trả về
        query.select(root);

        //search
        Predicate searchByName = builder.like(root.get("customer").get("name"), "%" + search + "%");
        Predicate searchByPhone = builder.like(root.get("customer").get("phone"), "%" + search + "%");
        Predicate searchByEmail = builder.like(root.get("customer").get("email"), "%" + search + "%");
        Predicate searchByTitle = builder.like(root.get("title"), "%" + search + "%");
        searchByTitle = builder.or(searchByTitle, searchByEmail, searchByPhone, searchByName);

        //tìm khoảng thời gian
        if (start != null) {
            Predicate findByGreaterTime = builder.greaterThanOrEqualTo(root.get("timeUpdated"), new Date(start));
            searchByTitle = builder.and(searchByTitle, findByGreaterTime);
        }
        if (end != null) {
            Predicate findByLessTime = builder.lessThanOrEqualTo(root.get("timeUpdated"), new Date(end));
            searchByTitle = builder.and(searchByTitle, findByLessTime);
        }

        //lọc theo diện tích
        if (minAcreage != null) {
            Predicate findByGreaterAcreage = builder.greaterThanOrEqualTo(root.get("acreage"), minAcreage);
            searchByTitle = builder.and(searchByTitle, findByGreaterAcreage);
        }
        if (maxAcreage != null) {
            Predicate findByLessAcreage = builder.lessThanOrEqualTo(root.get("acreage"), maxAcreage);
            searchByTitle = builder.and(searchByTitle, findByLessAcreage);
        }

        //tìm theo xã, huyện, tỉnh
        if (ward != null) {
            Predicate findByWard = builder.equal(root.get("ward").get("wardId"), ward);
            searchByTitle = builder.and(searchByTitle, findByWard);
        } else if (district != null) {
            Predicate findByDistrict = builder.equal(root.get("ward").get("district").get("districtId"), district);
            searchByTitle = builder.and(searchByTitle, findByDistrict);
        } else if (city != null) {
            Predicate findByCity = builder.equal(root.get("ward").get("district").get("city").get("cityId"), city);
            searchByTitle = builder.and(searchByTitle, findByCity);
        }

        //tìm theo roommate
        if (roommate != null) {
            if (roommate) {
                Predicate findByRoommateNotNull = builder.isNotNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNotNull);
            } else {
                Predicate findByRoommateNull = builder.isNull(root.get("roommate"));
                searchByTitle = builder.and(searchByTitle, findByRoommateNull);
            }
        }

        //tìm theo status
        if (status != null && !status.trim().equals("")) {
            switch (status) {
                case "uncheck":
                    Predicate findByStatusNull = builder.isNull(root.get("deleted"));
                    searchByTitle = builder.and(searchByTitle, findByStatusNull);
                    break;
                case "active":
                    Predicate findByStatusTrue = builder.isFalse(root.get("deleted"));
                    searchByTitle = builder.and(searchByTitle, findByStatusTrue);
                    break;
                case "hidden":
                    Predicate findByStatusFalse = builder.isTrue(root.get("deleted"));
                    searchByTitle = builder.and(searchByTitle, findByStatusFalse);
                    break;
            }
        }

        //tìm theo vip
        if (vip != null) {
            Predicate findByVip = builder.equal(root.get("vip"), vip);
            searchByTitle = builder.and(searchByTitle, findByVip);
        }

        query.where(searchByTitle);

        if (sort != null) {
            if (sort.equals("asc"))
                query.orderBy(builder.asc(root.get("timeUpdated")));
            else
                query.orderBy(builder.desc(root.get("timeUpdated")));
        }

        return em.createQuery(query).getResultList();
    }

    @Override
    public List<Article> findCustomNotHidden(String roomType, String title,
                                             Integer ward, Integer district, Integer city,
                                             Integer minPrice, Integer maxPrice,
                                             Integer minAcreage, Integer maxAcreage) {
        if (title == null || title.trim().equals(""))
            title = "%%";
        else
            title = "%" + title + "%";

        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Article> query = builder.createQuery(Article.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<Article> root = query.from(Article.class);

        //xác định cột trả về
        query.select(root);

        Predicate predicate = builder.like(root.get("title"), title);
        predicate = builder.and(predicate);

        //lọc theo khoảng giá
        if (minPrice != null) {
            Predicate findByGreaterPrice = builder.greaterThanOrEqualTo(root.get("roomPrice"), minPrice);
            predicate = builder.and(predicate, findByGreaterPrice);
        }
        if (maxPrice != null) {
            Predicate findByLessPrice = builder.lessThanOrEqualTo(root.get("roomPrice"), maxPrice);
            predicate = builder.and(predicate, findByLessPrice);
        }

        //lọc theo diện tích
        if (minAcreage != null) {
            Predicate findByGreaterAcreage = builder.greaterThanOrEqualTo(root.get("acreage"), minAcreage);
            predicate = builder.and(predicate, findByGreaterAcreage);
        }
        if (maxAcreage != null) {
            Predicate findByLessAcreage = builder.lessThanOrEqualTo(root.get("acreage"), maxAcreage);
            predicate = builder.and(predicate, findByLessAcreage);
        }

        //tìm theo xã, huyện, tỉnh
        if (ward != null) {
            Predicate findByWard = builder.equal(root.get("ward").get("wardId"), ward);
            predicate = builder.and(predicate, findByWard);
        } else if (district != null) {
            Predicate findByDistrict = builder.equal(root.get("ward").get("district").get("districtId"), district);
            predicate = builder.and(predicate, findByDistrict);
        } else if (city != null) {
            Predicate findByCity = builder.equal(root.get("ward").get("district").get("city").get("cityId"), city);
            predicate = builder.and(predicate, findByCity);
        }

        //tìm theo roommate
        if (roomType != null) {
            Predicate findByRoomType = builder.equal(root.get("roomType"), roomType);
            predicate = builder.and(predicate, findByRoomType);
        }

        //tìm theo status
        predicate = builder.and(predicate, builder.isFalse(root.get("deleted")));
        predicate = builder.and(predicate, builder.isFalse(root.get("blocked")));

        query.where(predicate);

        query.orderBy(builder.desc(root.get("timeUpdated")));
        query.orderBy(builder.desc(root.get("timeCreated")));

        return em.createQuery(query).getResultList();
    }

    @Override
    public List<Article> findCustomByEmail(Long start, Long end, String roomType,
                                           Integer ward, Integer district, Integer city,
                                           String status, Boolean vip, String title,
                                           Integer minAcreage, Integer maxAcreage,
                                           UserDetails currentUser) {
        if (title == null || title.trim().equals(""))
            title = "%%";
        else
            title = "%" + title + "%";

        //tạo builder
        CriteriaBuilder builder = em.getCriteriaBuilder();

        //tạo query
        CriteriaQuery<Article> query = builder.createQuery(Article.class);

        //xác định chủ thể cần truy vấn (=FROM Article)
        Root<Article> root = query.from(Article.class);

        //xác định cột trả về
        query.select(root);

        Predicate predicate = builder.like(root.get("title"), title);

        //phải đúng email người dùng
        Predicate findByEmail = builder.like(root.get("customer").get("email"), currentUser.getUsername());
        predicate = builder.and(predicate, findByEmail);


        //tìm khoảng thời gian
        if (start != null) {
            Predicate findByGreaterTime = builder.greaterThanOrEqualTo(root.<Date>get("timeUpdated"), new Date(start));
            predicate = builder.and(predicate, findByGreaterTime);
        }
        if (end != null) {
            Predicate findByLessTime = builder.lessThanOrEqualTo(root.<Date>get("timeUpdated"), new Date(end));
            predicate = builder.and(predicate, findByLessTime);
        }

        //lọc theo diện tích
        if (minAcreage != null) {
            Predicate findByGreaterAcreage = builder.greaterThanOrEqualTo(root.get("acreage"), minAcreage);
            predicate = builder.and(predicate, findByGreaterAcreage);
        }
        if (maxAcreage != null) {
            Predicate findByLessAcreage = builder.lessThanOrEqualTo(root.get("acreage"), maxAcreage);
            predicate = builder.and(predicate, findByLessAcreage);
        }

        //tìm theo xã, huyện, tỉnh
        if (ward != null) {
            Predicate findByWard = builder.equal(root.get("ward").get("wardId"), ward);
            predicate = builder.and(predicate, findByWard);
        } else if (district != null) {
            Predicate findByDistrict = builder.equal(root.get("ward").get("district").get("districtId"), district);
            predicate = builder.and(predicate, findByDistrict);
        } else if (city != null) {
            Predicate findByCity = builder.equal(root.get("ward").get("district").get("city").get("cityId"), city);
            predicate = builder.and(predicate, findByCity);
        }

        //tìm theo status
        if (status != null && !status.trim().equals("")) {
            switch (status) {
                case "unconfirmed":
                    Predicate findByStatusNull = builder.isNull(root.get("deleted"));
                    predicate = builder.and(predicate, findByStatusNull);
                    break;
                case "active":
                    Predicate findByStatusTrue = builder.isFalse(root.get("deleted"));
                    predicate = builder.and(predicate, findByStatusTrue);
                    break;
                case "hidden":
                    Predicate findByStatusFalse = builder.isTrue(root.get("deleted"));
                    predicate = builder.and(predicate, findByStatusFalse);
                    break;
            }
        }

        //tìm theo vip
        if (vip != null) {
            Predicate findByVip = builder.equal(root.get("vip"), vip);
            predicate = builder.and(predicate, findByVip);
        }

        query.where(predicate);

        return em.createQuery(query).getResultList();
    }
}
