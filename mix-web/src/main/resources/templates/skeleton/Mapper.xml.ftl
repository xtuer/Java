<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!-- namespace 非常重要：必须是 Mapper 类的全路径 -->
<mapper namespace="${package}.bean.Xxx">
    <!-- 注释在这儿 -->
    <select id="countUsersByAge" resultType="int">
    </select>
</mapper>
