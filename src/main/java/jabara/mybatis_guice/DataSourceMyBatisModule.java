package jabara.mybatis_guice;

import jabara.general.ArgUtil;
import jabara.general.ExceptionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;

import com.google.inject.Provider;
import com.google.inject.name.Names;

/**
 * @author jabaraster
 */
public class DataSourceMyBatisModule extends MyBatisModule {

    private final String         dataSourceJndiName;
    private final List<Class<?>> mapperClasses;

    /**
     * @param pDataSourceJndiName JNDIから{@link DataSource}をルックアップするときの名前.
     * @param pMapperClasses MyBatisのマッパクラスのリスト.
     */
    public DataSourceMyBatisModule(final String pDataSourceJndiName, final Class<?>... pMapperClasses) {
        ArgUtil.checkNullOrEmpty(pDataSourceJndiName, "pDataSourceJndiName"); //$NON-NLS-1$
        ArgUtil.checkNull(pMapperClasses, "pMapperClasses"); //$NON-NLS-1$
        this.dataSourceJndiName = pDataSourceJndiName;
        this.mapperClasses = Arrays.asList(pMapperClasses);
    }

    /**
     * @param pDataSourceJndiName JNDIから{@link DataSource}をルックアップするときの名前.
     * @param pMapperClasses MyBatisのマッパクラスのリスト.
     */
    public DataSourceMyBatisModule(final String pDataSourceJndiName, final Collection<Class<?>> pMapperClasses) {
        ArgUtil.checkNullOrEmpty(pDataSourceJndiName, "pDataSourceJndiName"); //$NON-NLS-1$
        ArgUtil.checkNull(pMapperClasses, "pMapperClasses"); //$NON-NLS-1$
        this.mapperClasses = new ArrayList<Class<?>>(pMapperClasses);
        this.dataSourceJndiName = pDataSourceJndiName;
    }

    /**
     * @return the dataSourceJndiName
     */
    public String getDataSourceJndiName() {
        return this.dataSourceJndiName;
    }

    /**
     * @return the mapperClasses
     */
    public List<Class<?>> getMapperClasses() {
        return new ArrayList<Class<?>>(this.mapperClasses);
    }

    /**
     * @see org.mybatis.guice.AbstractMyBatisModule#initialize()
     */
    @Override
    protected void initialize() {
        final DataSource ds = lookupDataSource();
        bindDataSourceProvider(new Provider<DataSource>() {
            @Override
            public DataSource get() {
                return ds;
            }
        });
        bindTransactionFactoryType(JdbcTransactionFactory.class);

        addMapperClasses(this.mapperClasses);

        final Map<String, String> map = putMyBatisProperty(new HashMap<String, String>(), toString());
        Names.bindProperties(binder(), map);
    }

    private DataSource lookupDataSource() {
        try {
            return InitialContext.doLookup(this.dataSourceJndiName);
        } catch (final NamingException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    /**
     * MyBatisが使う必須のプロパティを{@code pMap}に追加します.
     * 
     * @param pMap 追加するマップ.
     * @param pEnvironmentId キー{@code mybatis.environment.id}にセットする値.
     * @return 引数の{@code pMap}そのもの.
     */
    public static Map<String, String> putMyBatisProperty(final Map<String, String> pMap, final String pEnvironmentId) {
        pMap.put("mybatis.environment.id", pEnvironmentId); //$NON-NLS-1$ 
        pMap.put("JDBC.autoCommit", Boolean.toString(false)); //$NON-NLS-1$
        return pMap;
    }
}