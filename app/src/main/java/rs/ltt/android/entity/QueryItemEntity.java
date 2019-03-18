package rs.ltt.android.entity;

import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import rs.ltt.jmap.mua.entity.QueryResultItem;

@Entity(tableName = "query_item",
        foreignKeys = {@ForeignKey(entity = QueryEntity.class,
                parentColumns = {"id"},
                childColumns = {"queryId"}
        )},
        indices = {@Index(value = "queryId")}
)
public class QueryItemEntity {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @NonNull
    public Long queryId;

    //TODO: delete position; we just needed that for debugging stuff
    public Integer position;
    public String emailId;
    public String threadId;


    public QueryItemEntity(@NonNull Long queryId, Integer position, String emailId, String threadId) {
        this.queryId = queryId;
        this.position = position;
        this.emailId = emailId;
        this.threadId = threadId;
    }


    public static List<QueryItemEntity> of(final Long queryId, final QueryResultItem[] items) {
        ImmutableList.Builder<QueryItemEntity> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < items.length; ++i) {
            QueryResultItem item = items[i];
            builder.add(of(queryId, i, item));
        }
        return builder.build();
    }

    public static QueryItemEntity of(final Long queryId, Integer position, QueryResultItem item) {
        return new QueryItemEntity(queryId, position, item.getEmailId(), item.getThreadId());
    }
}