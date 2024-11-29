package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setMealDTO
     * @return
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setMealDTO) {
        Setmeal setMeal = new Setmeal();
        BeanUtils.copyProperties(setMealDTO, setMeal);

        //增加套餐
        setmealMapper.insert(setMeal);

        //获取套餐id
        Long id = setMeal.getId();

        List<SetmealDish> setmealDishes = setMealDTO.getSetmealDishes();
        for (SetmealDish setMealDish : setmealDishes) {
            setMealDish.setSetmealId(id);
        }

        // 保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE == setmeal.getStatus()){
                // 起售不能删除
                throw new
                        DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(id -> {
            //删除套餐中的数据
            setmealMapper.deleteBuId(id);
            //删除套餐中菜品的数据
            setmealDishMapper.deleteBySetmealId(id);
        });
    }
}
