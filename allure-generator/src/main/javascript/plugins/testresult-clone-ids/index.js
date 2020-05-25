import CloneCaseIdView from './CloneCaseIdView';

allure.api.addTestResultBlock(CloneCaseIdView, {position: 'before'});

allure.api.addTranslation('en', {
    cloneCase: {
        ids :{
            name: 'Clone case IDs'
        }
    }
});
