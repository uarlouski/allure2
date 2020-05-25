import './styles.scss';
import {View} from 'backbone.marionette';
import {className} from '../../decorators';
import template from './CloneCaseIdView.hbs';

@className('pane__section')
class CloneCaseIdView extends View {
    template = template;

    serializeData() {
        return {
            cloneCaseIds: this.model.get('labels').filter(l => l.name === 'cloneCaseId').map(l => l.value)
        };
    }
}

export default CloneCaseIdView;
