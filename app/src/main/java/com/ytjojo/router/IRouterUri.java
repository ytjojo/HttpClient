package com.ytjojo.router;

public interface IRouterUri {

    @RouterUri(routerUri = "xl://goods:8888/goodsDetail")//请求Url地址
    String jumpToGoodsDetail(@RouterParam("goodsId") String goodsId, @RouterParam("des") String des);//参数商品Id 商品描述

}