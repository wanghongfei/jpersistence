# JPersistence - A higher level abstraction for JPA

### What is the magic of JPersistence?
* It **dramatically reduces** the code you have to write to persist an entity.
* It **dramatically reduces** the code you have to write to query entities.

### How to use JPersistence?
* To do persistence stuff, you just need to extend your own class from `Home<T>`:
```
    @Scope("prototype")
    public class MemberHome extends Home<Member> {
    
    }
```
That's all code you need to write to do CRUD operation!
```
    memberHome.setInstance(newMemberInstance); // or memberHome.setInstanceId(1);
    memberHome.persist();
    memberHome.update();
    memberHome.delete();
    Member m = memberHome.getInstance();
```
You can customize query by override `customizeRestrictions()` method:
```
	@Override
	protected String[] customizeRestriction() {
		return new String[] {
				"obj.name = 'Bruce'",
				"obj.age = '18'",
				"obj.gender = 'male'"
		};
	}
```

* To do query stuff, you just need to extend your own class from `Query<T>`:
```
    public class MemberQuery extends Query<Member> {

    }
```
```
    memberQuery.getResultList();
    ... ...
```

Still in developing... ...
