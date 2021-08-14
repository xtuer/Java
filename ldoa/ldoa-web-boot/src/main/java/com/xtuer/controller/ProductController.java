package com.xtuer.controller;

import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.bean.User;
import com.xtuer.bean.product.Product;
import com.xtuer.bean.product.ProductFilter;
import com.xtuer.bean.product.ProductItem;
import com.xtuer.mapper.ProductMapper;
import com.xtuer.service.ProductService;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * 产品控制器
 */
@RestController
public class ProductController extends BaseController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 查询符合条件的产品
     * 网址: http://localhost:8080/api/products
     * 参数:
     *      name       [可选]: 名字
     *      code       [可选]: 编码
     *      model      [可选]: 规格/型号
     *      productIds [可选]: 产品 ID
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 数量
     *
     * @param filter 过滤条件
     * @param page   分页
     * @return payload 为查询到的产品数组
     */
    @GetMapping(Urls.API_PRODUCTS)
    public Result<List<Product>> findProducts(ProductFilter filter, Page page) {
        return Result.ok(productMapper.findProducts(filter, page));
    }

    /**
     * 导出符合条件的产品
     * 网址: http://localhost:8080/api/products/export
     * 参数:
     *      name       [可选]: 名字
     *      code       [可选]: 编码
     *      productIds [可选]: 产品 ID
     *
     * @param filter 过滤条件
     * @return payload 为导出的 Excel 文件的 URL
     */
    @GetMapping(Urls.API_PRODUCTS_EXPORT)
    public Result<String> exportProducts(ProductFilter filter) throws IOException {
        return Result.ok(productService.exportProducts(filter));
    }

    /**
     * 创建或者更新产品
     *
     * 网址: http://localhost:8080/api/products/{productId}
     * 参数: 无
     * 请求体: 产品的 JSON 字符串
     *     name  (必要): 产品名称
     *     code  (必要): 产品编码
     *     desc  [可选]: 产品描述
     *     model (必要): 产品规格/型号
     *     items (必要): [ { productItemId, count } ] 产品项的数组，但数组可以为空
     *
     * @param product 产品
     * @return payload 为更新后的产品
     */
    @PutMapping(Urls.API_PRODUCTS_BY_ID)
    public Result<Product> upsertProduct(@PathVariable long productId,
                                         @RequestBody @Valid Product product,
                                         BindingResult bindingResult) {
        // 如有参数错误，则返回错误信息给客户端
        if (bindingResult.hasErrors()) {
            return Result.fail(Utils.getBindingMessage(bindingResult));
        }

        User user = super.getCurrentUser();
        product.setProductId(productId);
        return productService.upsertProduct(product, user);
    }

    /**
     * 删除产品
     *
     * 网址: http://localhost:8080/api/products/{productId}
     * 参数: 无
     *
     * @param productId 产品 ID
     */
    @DeleteMapping(Urls.API_PRODUCTS_BY_ID)
    public Result<Boolean> deleteProduct(@PathVariable long productId) {
        productService.deleteProduct(productId);
        return Result.ok();
    }

    /**
     * 查询符合条件的产品项
     *
     * 网址: http://localhost:8080/api/productItems
     * 参数:
     *      name       [可选]: 物料名称
     *      code       [可选]: 物料编码
     *      model      [可选]: 规格型号
     *      count      [可选]: 数量 (大于 0 时查询小于等于 count 的产品项)
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 数量
     *
     * @param filter 过滤条件
     * @param page   分页
     * @return payload 为产品项的数组
     */
    @GetMapping(Urls.API_PRODUCT_ITEMS)
    public Result<List<ProductItem>> findProductItems(ProductItem filter, Page page) {
        return Result.ok(productMapper.findProductItems(filter, page));
    }

    /**
     * 创建或者更新产品项 (物料)
     *
     * 网址: http://localhost:8080/api/productItems/{productItemId}
     * 参数:
     *      name      (必要): 物料名称
     *      code      (必要): 物料编码
     *      type      (必要): 物料类型
     *      desc      [可选]: 物料描述
     *      model     (必要): 物料规格/型号
     *      standard  (必要): 标准/规范
     *      material  (必要): 材质
     *      warnCount (必要): 库存告警数量
     *
     * @param productItemId 产品项 ID
     * @param item          产品项
     * @return payload 为更新后的产品项
     */
    @PutMapping(Urls.API_PRODUCT_ITEMS_BY_ID)
    public Result<ProductItem> upsertProductItem(@PathVariable long productItemId,
                                                 @Valid ProductItem item,
                                                 BindingResult bindingResult) {
        // 如有参数错误，则返回错误信息给客户端
        if (bindingResult.hasErrors()) {
            return Result.fail(Utils.getBindingMessage(bindingResult));
        }

        User user = super.getCurrentUser();
        item.setProductItemId(productItemId);
        return productService.upsertProductItem(item, user);
    }

    /**
     * 删除产品项
     *
     * 网址: http://localhost:8080/api/productItems/{productItemId}
     * 参数: 无
     *
     * @param productItemId 产品项 ID
     */
    @DeleteMapping(Urls.API_PRODUCT_ITEMS_BY_ID)
    public Result<Boolean> deleteProductItem(@PathVariable long productItemId) {
        productMapper.deleteProductItem(productItemId);
        return Result.ok();
    }
}
