package net.cattweasel.pokebot.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.persistence.Entity;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.util.ClassUtils;

public class ExtendedAnnotationFactoryBean extends LocalSessionFactoryBean {

	private String[] basePackages;
	private ClassLoader beanClassLoader;

	public void setBasePackage(String basePackage) {
		this.basePackages = new String[] { basePackage };
	}

	public void setBasePackages(String[] basePackages) {
		this.basePackages = basePackages == null
				? null : Arrays.copyOf(basePackages, basePackages.length);
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	public void afterPropertiesSet() throws IOException {
		Collection<Class<?>> entities = new ArrayList<Class<?>>();
		ClassPathScanningCandidateComponentProvider scanner = this.createScanner();
		for (String basePackage : this.basePackages) {
			this.findEntities(scanner, entities, basePackage);
		}
		this.setAnnotatedClasses(entities.toArray(new Class<?>[entities.size()]));
		this.setAnnotatedPackages(this.basePackages);
		super.afterPropertiesSet();
	}

	private ClassPathScanningCandidateComponentProvider createScanner() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		return scanner;
	}

	private void findEntities(ClassPathScanningCandidateComponentProvider scanner, Collection<Class<?>> entities,
			String basePackage) {
		Set<BeanDefinition> annotatedClasses = scanner.findCandidateComponents(basePackage);
		for (BeanDefinition bd : annotatedClasses) {
			String className = bd.getBeanClassName();
			Class<?> type = ClassUtils.resolveClassName(className, this.beanClassLoader);
			entities.add(type);
		}
	}
}
