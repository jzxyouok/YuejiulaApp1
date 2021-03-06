package com.liangxun.yuejiula.db;

import de.greenrobot.dao.DaoException;

import java.io.Serializable;


// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table SHOPPING_CART.
 */
public class ShoppingCart implements Serializable{

    /** Not-null value. */
    private String cartid;
    /** Not-null value. */
    private String goods_id;
    /** Not-null value. */
    private String emp_id;
    /** Not-null value. */
    private String emp_name;
    /** Not-null value. */
    private String emp_cover;
    /** Not-null value. */
    private String goods_name;
    /** Not-null value. */
    private String goods_cover;
    /** Not-null value. */
    private String sell_price;
    /** Not-null value. */
    private String goods_count;
    /** Not-null value. */
    private String dateline;
    /** Not-null value. */
    private String is_select;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient ShoppingCartDao myDao;


    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public ShoppingCart() {
    }

    public ShoppingCart(String cartid) {
        this.cartid = cartid;
    }

    public ShoppingCart(String cartid, String goods_id, String emp_id, String emp_name, String emp_cover, String goods_name, String goods_cover, String sell_price, String goods_count, String dateline, String is_select) {
        this.cartid = cartid;
        this.goods_id = goods_id;
        this.emp_id = emp_id;
        this.emp_name = emp_name;
        this.emp_cover = emp_cover;
        this.goods_name = goods_name;
        this.goods_cover = goods_cover;
        this.sell_price = sell_price;
        this.goods_count = goods_count;
        this.dateline = dateline;
        this.is_select = is_select;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getShoppingCartDao() : null;
    }

    /** Not-null value. */
    public String getCartid() {
        return cartid;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCartid(String cartid) {
        this.cartid = cartid;
    }

    /** Not-null value. */
    public String getGoods_id() {
        return goods_id;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    /** Not-null value. */
    public String getEmp_id() {
        return emp_id;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    /** Not-null value. */
    public String getEmp_name() {
        return emp_name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmp_name(String emp_name) {
        this.emp_name = emp_name;
    }

    /** Not-null value. */
    public String getEmp_cover() {
        return emp_cover;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmp_cover(String emp_cover) {
        this.emp_cover = emp_cover;
    }

    /** Not-null value. */
    public String getGoods_name() {
        return goods_name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    /** Not-null value. */
    public String getGoods_cover() {
        return goods_cover;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGoods_cover(String goods_cover) {
        this.goods_cover = goods_cover;
    }

    /** Not-null value. */
    public String getSell_price() {
        return sell_price;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSell_price(String sell_price) {
        this.sell_price = sell_price;
    }

    /** Not-null value. */
    public String getGoods_count() {
        return goods_count;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGoods_count(String goods_count) {
        this.goods_count = goods_count;
    }

    /** Not-null value. */
    public String getDateline() {
        return dateline;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDateline(String dateline) {
        this.dateline = dateline;
    }

    /** Not-null value. */
    public String getIs_select() {
        return is_select;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setIs_select(String is_select) {
        this.is_select = is_select;
    }

    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
