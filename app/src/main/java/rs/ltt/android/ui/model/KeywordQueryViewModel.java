/*
 * Copyright 2019 Daniel Gultsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rs.ltt.android.ui.model;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.ListenableFuture;

import rs.ltt.android.entity.AccountWithCredentials;
import rs.ltt.jmap.common.entity.filter.EmailFilterCondition;
import rs.ltt.jmap.common.entity.query.EmailQuery;

public class KeywordQueryViewModel extends AbstractQueryViewModel {

    private final LiveData<EmailQuery> emailQueryLiveData;

    private final String keyword;

    KeywordQueryViewModel(final Application application, final long accountId, @NonNull final String keyword) {
        super(application, accountId);
        this.keyword = keyword;
        //TODO; we probably want to change this to someInThreadHaveKeyword
        this.emailQueryLiveData = Transformations.map(queryRepository.getTrashAndJunk(), trashAndJunk -> EmailQuery.of(
                EmailFilterCondition.builder().hasKeyword(keyword).inMailboxOtherThan(trashAndJunk).build(),
                true
        ));
        init();
    }

    public String getKeyword() {
        return this.keyword;
    }

    @Override
    protected LiveData<EmailQuery> getQuery() {
        return emailQueryLiveData;
    }
}
