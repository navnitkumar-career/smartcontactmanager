package com.example.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entities.MyOrder;


public interface MyOrderRepository extends JpaRepository<MyOrder, Integer>{

	@Query(value = "select * from my_order  where order_id= :orderId", nativeQuery = true)
	MyOrder fingByOrderId(@Param("orderId") String orderId);
}
