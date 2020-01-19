package java.lang;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.scope.ClassScope;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.AccessController;

/**
 * @Author: czw
 * @CreateTime: 2020-01-18 11:00
 * @UpdeteTime: 2020-01-18 11:00
 * @Description:
 */
public final class Class<T> implements Serializable,
		// 获取参数类型
		GenericDeclaration, Type ,
		// 获取class上的annotations信息
		AnnotatedElement {
	private static final int ANNOTATION= 0x00002000;
	private static final int ENUM      = 0x00004000;
	private static final int SYNTHETIC = 0x00001000;

	private static native void registerNatives();
	// 私有的构造函数，仅java虚拟机能创建它的实例，构造函数不能被使用，
	// 作用时防止默认构造函数生成
	private Class(ClassLoader classLoader){
		//初始化final字段ClassLoader，非空的初始化值能阻止以后JIT（just in time compiler）
		// 做出假设final字段为空的优化
		this.classLoader = classLoader;
	}
	@Override
	public String toString(){
		return (isInterface()?"interface":(isPrimitive()?"":"class"))+getName();
	}
	static {
		registerNatives();
	}

	public String getName(){
		String name=this.name;
		if (name==null)
			this.name=name=getName0();
		return name;
	}
	private native String getName0();
	//将类的名称缓存在name字段，减少对vm的调用次数
	private transient String name;
	// 确保这些方法的调用者可靠地被发现的一种机制，callerSensitive方法会根据其调用者的类型
	// 改变其行为，Reflection.getCallerClass方法可以获得调用者的class类型
	@CallerSensitive
	public ClassLoader getClassLoader(){
		ClassLoader cl=getClassLoader0();
		if (cl==null)
			return null;
		SecurityManager sm=System.getSecurityManager();
		if (sm!=null)
			ClassLoader.checkClassLoaderPermission(cl, Reflection.getCallerClass());
		return cl;
	}
	ClassLoader getClassLoader0(){
		return classLoader;
	}
	// 不是在私有构造器在jvm初始化的
	private final ClassLoader classLoader;
	// 类信息仓库，采用懒加载初始化
	private volatile transient ClassRepository genericInfo;

	@SuppressWarnings("unchecked")
	public TypeVariable<Class<T>>[] getTypeParameters() {
		ClassRepository info=getGenericInfo();
		if (info!=null)
			return (TypeVariable<Class<T>>[])info.getTypeParameters();
		else
			return (TypeVariable<Class<T>>[])new TypeVariable<?>[0];
	}
	//工厂通道
	private GenericsFactory getFactory(){
		// 新建作用域和工厂
		// coreReflectionFactory是genericsFactory接口的实现类，泛型工厂，提供了
		// Type的子接口类型工厂和java基础数据类型工厂接口---待续
		return CoreReflectionFactory.make(this, ClassScope.make(this));
	}

	// 类信息仓库的访问通道，类信息是懒加载初始化的
	// classRepository是单例类，据说为了缓存，不用每次都去jvm获取类型---待续
	private ClassRepository getGenericInfo(){
		ClassRepository genericInfo=this.genericInfo;
		if (genericInfo==null){
			String signature=getGenericSignature0();
			if (signature==null){
				genericInfo=ClassRepository.NONE;
			}else {
				genericInfo=ClassRepository.make(signature,getFactory());
			}
			this.genericInfo=genericInfo;
		}
		return (genericInfo!=ClassRepository.NONE)?genericInfo:null;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	public Annotation[] getAnnotations() {
		return new Annotation[0];
	}

	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}
	// 注解控制
	native byte[] getRawAnnotations();
	// since 1.8
	native byte[] getRawTypeAnnotations();
	//static byte[] getExecutableTypeAnnotationBytes(Executable ex){
	//	return
	//}
	// 为反射类获取工厂
	private static ReflectionFactory getReflectionFactory(){
		if (reflectionFactory==null){
			reflectionFactory= AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
		}
		return reflectionFactory;
	}

	private static ReflectionFactory reflectionFactory;
	private native String getGenericSignature0();
	public native boolean isInterface();
	public native boolean isPrimitive();
	public static void main(String[] args) {

	}
}
