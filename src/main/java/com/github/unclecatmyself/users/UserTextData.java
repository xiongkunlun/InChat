package com.github.unclecatmyself.users;

import com.github.unclecatmyself.common.utils.SpringContextUtils;
import com.github.unclecatmyself.task.TextData;
import com.github.unclecatmyself.users.pojo.Test;
import com.github.unclecatmyself.users.repository.TestRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by MySelf on 2019/8/20.
 */
@Component
@DependsOn("springContextUtils")
public class UserTextData extends TextData {

    private TestRepository repository = (TestRepository) SpringContextUtils.getBean(TestRepository.class);

    @Override
    public void writeData(Map<String, Object> maps) {
        Test test = new Test();
        test.setId(1);
        test.setMsg("1111");
        System.out.println(test.toString());
        repository.save(test);
    }
}
